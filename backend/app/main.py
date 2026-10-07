from contextlib import asynccontextmanager
from typing import Optional
from fastapi import FastAPI, Depends, HTTPException, Query, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from .database import engine, Base, get_db
from .models import NumberReputation, DomainReputation, CommunityReport, current_time_millis
from .schemas import (
    CheckNumberRequest,
    NumberReputationResponse,
    CheckDomainRequest,
    DomainReputationResponse,
    ReportNumberRequest,
    ReportDomainRequest,
    ReportSubmissionResponse,
    ThreatUpdatesResponse,
)
from .scoring import (
    normalize_phone_number,
    normalize_domain,
    compute_confidence,
    compute_updated_risk_score,
)
from .seed import seed_database_if_empty


# Ensure database tables exist immediately
Base.metadata.create_all(bind=engine)
with Session(engine) as init_session:
    seed_database_if_empty(init_session)


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    with Session(engine) as session:
        seed_database_if_empty(session)
    yield


app = FastAPI(
    title="Suraksha Saathi Threat Intelligence API",
    description="Privacy-preserving backend service for scam reputation checks, community reports, and threat updates.",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def root():
    return {
        "service": "Suraksha Saathi Cloud Threat Engine",
        "status": "active",
        "version": "1.0.0",
        "endpoints": [
            "POST /check-number",
            "POST /check-domain",
            "POST /report-number",
            "POST /report-domain",
            "GET /threat-updates",
        ],
    }


@app.get("/health")
def health(db: Session = Depends(get_db)):
    num_count = db.query(NumberReputation).count()
    dom_count = db.query(DomainReputation).count()
    return {
        "status": "healthy",
        "known_scam_numbers": num_count,
        "known_malicious_domains": dom_count,
        "timestamp": current_time_millis(),
    }


# ─────────────────────────────────────────────────────────────
# Endpoint 1: POST /check-number
# ─────────────────────────────────────────────────────────────
@app.post("/check-number", response_model=NumberReputationResponse)
def check_number(req: CheckNumberRequest, db: Session = Depends(get_db)):
    clean_number = normalize_phone_number(req.number)
    
    # Query database for exact match or normalized variants
    record = (
        db.query(NumberReputation)
        .filter(NumberReputation.number == clean_number)
        .first()
    )

    if not record and clean_number.startswith("+91"):
        raw_ten_digit = clean_number.removeprefix("+91")
        record = (
            db.query(NumberReputation)
            .filter(NumberReputation.number == raw_ten_digit)
            .first()
        )

    if record:
        return NumberReputationResponse(
            number=record.number,
            category=record.category,
            riskScore=record.risk_score,
            reportCount=record.report_count,
            confidence=record.confidence,
            isScam=record.risk_score >= 50,
            updatedAt=record.updated_at,
        )

    # If not present in known database, return safe baseline
    return NumberReputationResponse(
        number=clean_number,
        category="Unflagged / No community reports",
        riskScore=0,
        reportCount=0,
        confidence=0.1,
        isScam=False,
        updatedAt=current_time_millis(),
    )


# ─────────────────────────────────────────────────────────────
# Endpoint 2: POST /check-domain
# ─────────────────────────────────────────────────────────────
@app.post("/check-domain", response_model=DomainReputationResponse)
def check_domain(req: CheckDomainRequest, db: Session = Depends(get_db)):
    clean_domain = normalize_domain(req.domain)
    
    record = (
        db.query(DomainReputation)
        .filter(DomainReputation.domain == clean_domain)
        .first()
    )

    if record:
        return DomainReputationResponse(
            domain=record.domain,
            category=record.category,
            riskScore=record.risk_score,
            reportCount=record.report_count,
            confidence=record.confidence,
            isScam=record.risk_score >= 50,
            updatedAt=record.updated_at,
        )

    return DomainReputationResponse(
        domain=clean_domain,
        category="Unflagged / No community reports",
        riskScore=0,
        reportCount=0,
        confidence=0.1,
        isScam=False,
        updatedAt=current_time_millis(),
    )


# ─────────────────────────────────────────────────────────────
# Endpoint 3: POST /report-number
# ─────────────────────────────────────────────────────────────
@app.post("/report-number", response_model=ReportSubmissionResponse, status_code=status.HTTP_201_CREATED)
def report_number(req: ReportNumberRequest, db: Session = Depends(get_db)):
    clean_number = normalize_phone_number(req.number)
    now = current_time_millis()

    # Log the raw community report
    audit_report = CommunityReport(
        target=clean_number,
        target_type="number",
        category=req.category,
        description=req.description,
        reporter_id=req.reporter_id,
        created_at=now,
    )
    db.add(audit_report)

    # Upsert into threat reputation index
    record = (
        db.query(NumberReputation)
        .filter(NumberReputation.number == clean_number)
        .first()
    )

    if record:
        record.report_count += 1
        record.category = req.category
        record.risk_score = compute_updated_risk_score(record.risk_score, record.report_count, req.category)
        record.confidence = compute_confidence(record.report_count)
        record.updated_at = now
    else:
        new_risk = compute_updated_risk_score(60, 1, req.category)
        record = NumberReputation(
            number=clean_number,
            category=req.category,
            risk_score=new_risk,
            report_count=1,
            confidence=compute_confidence(1),
            created_at=now,
            updated_at=now,
        )
        db.add(record)

    db.commit()
    db.refresh(record)

    return ReportSubmissionResponse(
        status="success",
        message="Threat report registered and threat score recalibrated.",
        target=record.number,
        targetType="number",
        newReportCount=record.report_count,
        riskScore=record.risk_score,
        confidence=record.confidence,
    )


# ─────────────────────────────────────────────────────────────
# Endpoint 4: POST /report-domain
# ─────────────────────────────────────────────────────────────
@app.post("/report-domain", response_model=ReportSubmissionResponse, status_code=status.HTTP_201_CREATED)
def report_domain(req: ReportDomainRequest, db: Session = Depends(get_db)):
    clean_domain = normalize_domain(req.domain)
    now = current_time_millis()

    # Log raw community report
    audit_report = CommunityReport(
        target=clean_domain,
        target_type="domain",
        category=req.category,
        description=req.description,
        reporter_id=req.reporter_id,
        created_at=now,
    )
    db.add(audit_report)

    # Upsert into threat reputation index
    record = (
        db.query(DomainReputation)
        .filter(DomainReputation.domain == clean_domain)
        .first()
    )

    if record:
        record.report_count += 1
        record.category = req.category
        record.risk_score = compute_updated_risk_score(record.risk_score, record.report_count, req.category)
        record.confidence = compute_confidence(record.report_count)
        record.updated_at = now
    else:
        new_risk = compute_updated_risk_score(65, 1, req.category)
        record = DomainReputation(
            domain=clean_domain,
            category=req.category,
            risk_score=new_risk,
            report_count=1,
            confidence=compute_confidence(1),
            created_at=now,
            updated_at=now,
        )
        db.add(record)

    db.commit()
    db.refresh(record)

    return ReportSubmissionResponse(
        status="success",
        message="Malicious domain report registered and threat score recalibrated.",
        target=record.domain,
        targetType="domain",
        newReportCount=record.report_count,
        riskScore=record.risk_score,
        confidence=record.confidence,
    )


# ─────────────────────────────────────────────────────────────
# Endpoint 5: GET /threat-updates
# ─────────────────────────────────────────────────────────────
@app.get("/threat-updates", response_model=ThreatUpdatesResponse)
def get_threat_updates(
    since_timestamp: Optional[int] = Query(0, alias="since_timestamp", description="Filter records modified after epoch millis"),
    db: Session = Depends(get_db),
):
    """
    Returns delta threat intelligence modified since the given timestamp.
    Used by Android client to keep offline LocalReputationDb synchronized.
    """
    now = current_time_millis()

    numbers_query = db.query(NumberReputation)
    domains_query = db.query(DomainReputation)

    if since_timestamp and since_timestamp > 0:
        numbers_query = numbers_query.filter(NumberReputation.updated_at >= since_timestamp)
        domains_query = domains_query.filter(DomainReputation.updated_at >= since_timestamp)

    numbers = numbers_query.all()
    domains = domains_query.all()

    return ThreatUpdatesResponse(
        serverTime=now,
        totalNumbers=len(numbers),
        totalDomains=len(domains),
        numbers=[
            NumberReputationResponse(
                number=n.number,
                category=n.category,
                riskScore=n.risk_score,
                reportCount=n.report_count,
                confidence=n.confidence,
                isScam=n.risk_score >= 50,
                updatedAt=n.updated_at,
            )
            for n in numbers
        ],
        domains=[
            DomainReputationResponse(
                domain=d.domain,
                category=d.category,
                riskScore=d.risk_score,
                reportCount=d.report_count,
                confidence=d.confidence,
                isScam=d.risk_score >= 50,
                updatedAt=d.updated_at,
            )
            for d in domains
        ],
    )
