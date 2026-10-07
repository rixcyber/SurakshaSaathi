import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health():
    res = client.get("/health")
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "healthy"
    assert data["known_scam_numbers"] >= 5
    assert data["known_malicious_domains"] >= 5


def test_check_number_known_and_unknown():
    # Known scam number
    res = client.post("/check-number", json={"number": "+919876543210"})
    assert res.status_code == 200
    data = res.json()
    assert data["isScam"] is True
    assert data["riskScore"] >= 80
    assert "SBI" in data["category"]

    # Unknown clean number
    res2 = client.post("/check-number", json={"number": "+919999999999"})
    assert res2.status_code == 200
    data2 = res2.json()
    assert data2["isScam"] is False
    assert data2["riskScore"] == 0


def test_check_domain_known_and_unknown():
    # Known phishing domain
    res = client.post("/check-domain", json={"domain": "https://sbi-login-verification.xyz/login"})
    assert res.status_code == 200
    data = res.json()
    assert data["isScam"] is True
    assert data["riskScore"] >= 90

    # Clean domain
    res2 = client.post("/check-domain", json={"domain": "https://google.com"})
    assert res2.status_code == 200
    data2 = res2.json()
    assert data2["isScam"] is False


def test_report_number_and_recalibrate():
    test_number = "+919111222333"
    res = client.post(
        "/report-number",
        json={
            "number": test_number,
            "category": "Fake CBI digital arrest threat",
            "description": "Demanded 50,000 INR on video call",
            "reporterId": "device_hash_xyz123",
        },
    )
    assert res.status_code == 201
    data = res.json()
    assert data["status"] == "success"
    assert data["newReportCount"] >= 1
    assert data["riskScore"] >= 60

    # Check that subsequent check returns the newly reported threat
    check_res = client.post("/check-number", json={"number": test_number})
    assert check_res.status_code == 200
    check_data = check_res.json()
    assert check_data["isScam"] is True
    assert "CBI" in check_data["category"]


def test_report_domain():
    test_dom = "https://tatapower-bill-discount.online/pay"
    res = client.post(
        "/report-domain",
        json={
            "domain": test_dom,
            "category": "Fake Tata Power electricity bill phishing link",
            "description": "SMS stated power cut at 9:30 PM",
        },
    )
    assert res.status_code == 201
    data = res.json()
    assert data["target"] == "tatapower-bill-discount.online"
    assert data["riskScore"] >= 65


def test_threat_updates_delta_sync():
    res = client.get("/threat-updates?since_timestamp=0")
    assert res.status_code == 200
    data = res.json()
    assert data["totalNumbers"] >= 5
    assert data["totalDomains"] >= 5
    assert len(data["numbers"]) >= 5
    assert len(data["domains"]) >= 5


if __name__ == "__main__":
    print("Running automated API test suite...")
    test_health()
    print("[OK] Health check passed")
    test_check_number_known_and_unknown()
    print("[OK] Check number endpoint passed")
    test_check_domain_known_and_unknown()
    print("[OK] Check domain endpoint passed")
    test_report_number_and_recalibrate()
    print("[OK] Report number and risk recalibration passed")
    test_report_domain()
    print("[OK] Report domain passed")
    test_threat_updates_delta_sync()
    print("[OK] Threat updates sync endpoint passed")
    print("\nALL 5 ENDPOINTS TESTED & PASSED PERFECTLY!")
