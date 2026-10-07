from sqlalchemy.orm import Session
from .models import NumberReputation, DomainReputation, current_time_millis
from .scoring import compute_confidence

INITIAL_NUMBERS = [
    {
        "number": "+919876543210",
        "category": "Fake bank call — pretends to be SBI customer care",
        "risk_score": 80,
        "report_count": 45,
    },
    {
        "number": "+919876543211",
        "category": "Suspicious — high-return crypto / investment fraud",
        "risk_score": 55,
        "report_count": 12,
    },
    {
        "number": "+919876543212",
        "category": "Digital Arrest impersonation — pretends to be Delhi Police / CBI",
        "risk_score": 90,
        "report_count": 78,
    },
    {
        "number": "+918001234567",
        "category": "Fake lottery / KBC reward scam",
        "risk_score": 75,
        "report_count": 34,
    },
    {
        "number": "+919123456789",
        "category": "Electricity bill disconnection threat / fake officer",
        "risk_score": 85,
        "report_count": 52,
    },
]

INITIAL_DOMAINS = [
    {
        "domain": "sbi-login-verification.xyz",
        "category": "Fake bank phishing portal stealing credentials",
        "risk_score": 95,
        "report_count": 112,
    },
    {
        "domain": "bit.ly/sbi-kyc",
        "category": "Bank KYC credential & PAN harvesting shortlink",
        "risk_score": 90,
        "report_count": 67,
    },
    {
        "domain": "electricity-bill-pay.site",
        "category": "Fake utility payment portal with extortion lock",
        "risk_score": 85,
        "report_count": 43,
    },
    {
        "domain": "free-giftcard-claim.click",
        "category": "WhatsApp viral scam reward link harvesting contacts",
        "risk_score": 75,
        "report_count": 38,
    },
    {
        "domain": "apk-police-challan-pay.online",
        "category": "Malicious APK dropper disguised as e-Challan app",
        "risk_score": 98,
        "report_count": 140,
    },
]


def seed_database_if_empty(db: Session):
    """Populates threat intelligence database if empty."""
    now = current_time_millis()

    if db.query(NumberReputation).count() == 0:
        for item in INITIAL_NUMBERS:
            conf = compute_confidence(item["report_count"])
            record = NumberReputation(
                number=item["number"],
                category=item["category"],
                risk_score=item["risk_score"],
                report_count=item["report_count"],
                confidence=conf,
                created_at=now,
                updated_at=now,
            )
            db.add(record)
        db.commit()

    if db.query(DomainReputation).count() == 0:
        for item in INITIAL_DOMAINS:
            conf = compute_confidence(item["report_count"])
            record = DomainReputation(
                domain=item["domain"],
                category=item["category"],
                risk_score=item["risk_score"],
                report_count=item["report_count"],
                confidence=conf,
                created_at=now,
                updated_at=now,
            )
            db.add(record)
        db.commit()
