from typing import List, Optional
from pydantic import BaseModel, Field


# --- Check Requests & Responses ---

class CheckNumberRequest(BaseModel):
    number: str = Field(..., description="Phone number with or without country code", example="+919876543210")


class NumberReputationResponse(BaseModel):
    number: str
    category: str
    risk_score: int = Field(..., alias="riskScore")
    report_count: int = Field(..., alias="reportCount")
    confidence: float
    is_scam: bool = Field(..., alias="isScam")
    updated_at: int = Field(..., alias="updatedAt")

    class Config:
        populate_by_name = True
        from_attributes = True


class CheckDomainRequest(BaseModel):
    domain: str = Field(..., description="Domain name or URL", example="sbi-login-verification.xyz")


class DomainReputationResponse(BaseModel):
    domain: str
    category: str
    risk_score: int = Field(..., alias="riskScore")
    report_count: int = Field(..., alias="reportCount")
    confidence: float
    is_scam: bool = Field(..., alias="isScam")
    updated_at: int = Field(..., alias="updatedAt")

    class Config:
        populate_by_name = True
        from_attributes = True


# --- Report Requests & Responses ---

class ReportNumberRequest(BaseModel):
    number: str = Field(..., example="+919876543299")
    category: str = Field(..., example="Fake electricity bill disconnection extortion")
    description: Optional[str] = Field(None, example="Caller threatened disconnection in 2 hours")
    reporter_id: Optional[str] = Field(None, alias="reporterId")


class ReportDomainRequest(BaseModel):
    domain: str = Field(..., example="pay-electricity-bill-urgent.top")
    category: str = Field(..., example="Phishing site stealing debit card OTP")
    description: Optional[str] = Field(None, example="Link received via SMS")
    reporter_id: Optional[str] = Field(None, alias="reporterId")


class ReportSubmissionResponse(BaseModel):
    status: str
    message: str
    target: str
    target_type: str = Field(..., alias="targetType")
    new_report_count: int = Field(..., alias="newReportCount")
    risk_score: int = Field(..., alias="riskScore")
    confidence: float

    class Config:
        populate_by_name = True


# --- Threat Updates Sync ---

class ThreatUpdatesResponse(BaseModel):
    server_time: int = Field(..., alias="serverTime")
    total_numbers: int = Field(..., alias="totalNumbers")
    total_domains: int = Field(..., alias="totalDomains")
    numbers: List[NumberReputationResponse]
    domains: List[DomainReputationResponse]

    class Config:
        populate_by_name = True
