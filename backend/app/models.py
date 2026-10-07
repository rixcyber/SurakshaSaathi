import time
from sqlalchemy import Column, Integer, String, Float, Text, BigInteger
from .database import Base


def current_time_millis() -> int:
    return int(time.time() * 1000)


class NumberReputation(Base):
    __tablename__ = "number_reputations"

    id = Column(Integer, primary_key=True, index=True)
    number = Column(String(32), unique=True, index=True, nullable=False)
    category = Column(String(255), nullable=False)
    risk_score = Column(Integer, default=50) # 0 - 100
    report_count = Column(Integer, default=1)
    confidence = Column(Float, default=0.5)   # 0.0 - 1.0
    created_at = Column(BigInteger, default=current_time_millis)
    updated_at = Column(BigInteger, default=current_time_millis, onupdate=current_time_millis)


class DomainReputation(Base):
    __tablename__ = "domain_reputations"

    id = Column(Integer, primary_key=True, index=True)
    domain = Column(String(255), unique=True, index=True, nullable=False)
    category = Column(String(255), nullable=False)
    risk_score = Column(Integer, default=50) # 0 - 100
    report_count = Column(Integer, default=1)
    confidence = Column(Float, default=0.5)   # 0.0 - 1.0
    created_at = Column(BigInteger, default=current_time_millis)
    updated_at = Column(BigInteger, default=current_time_millis, onupdate=current_time_millis)


class CommunityReport(Base):
    __tablename__ = "community_reports"

    id = Column(Integer, primary_key=True, index=True)
    target = Column(String(255), nullable=False, index=True) # Phone or domain
    target_type = Column(String(32), nullable=False)         # "number", "domain", "message"
    category = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    reporter_id = Column(String(64), nullable=True)          # Anonymized device/reporter hash
    created_at = Column(BigInteger, default=current_time_millis)
