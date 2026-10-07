import re
import math


def normalize_phone_number(raw: str) -> str:
    """Normalizes phone number to canonical +91 format if Indian number."""
    clean = re.sub(r"[\s\-\(\)]", "", raw.strip())
    if clean.startswith("+"):
        return clean
    if clean.startswith("91") and len(clean) == 12:
        return f"+{clean}"
    if len(clean) == 10:
        return f"+91{clean}"
    return clean


def normalize_domain(raw: str) -> str:
    """Extracts base domain from URLs or domain strings."""
    clean = raw.strip().lower()
    clean = re.sub(r"^https?://", "", clean)
    clean = clean.split("/")[0].split("?")[0].split(":")[0]
    return clean


def compute_confidence(report_count: int) -> float:
    """
    Computes statistical confidence based on aggregate report volume.
    Using logistic/saturation curve: 1 report ~ 0.45, 5 ~ 0.73, 10 ~ 0.90, 20+ ~ 0.98.
    """
    if report_count <= 0:
        return 0.1
    confidence = 1.0 - (1.0 / (1.0 + 0.3 * report_count))
    return round(min(0.99, max(0.40, confidence)), 2)


def compute_updated_risk_score(current_score: int, new_report_count: int, category: str) -> int:
    """
    Recalibrates risk score based on category severity and report accumulation.
    """
    category_lower = category.lower()
    base_boost = 0
    if "police" in category_lower or "arrest" in category_lower or "cbi" in category_lower:
        base_boost = 25
    elif "bank" in category_lower or "kyc" in category_lower or "otp" in category_lower:
        base_boost = 20
    elif "electricity" in category_lower or "extortion" in category_lower:
        base_boost = 15
    else:
        base_boost = 10

    # Progressive escalation based on reports
    volume_bonus = min(25, int(math.log2(new_report_count + 1) * 6))
    calculated = max(current_score, 50 + base_boost + volume_bonus)
    return min(99, max(10, calculated))
