import { startPortoneCharge } from "../services/portoneService";

export default function PortoneTestButton() {
    const onClick = async () => {
        try {
            console.log("[PortOne] click");
            const res = await startPortoneCharge({
                amount: 100,
                payMethod: "card",
                pg: "html5_inicis",
                buyer: { name: "홍길동", email: "buyer@test.com", tel: "010-1234-5678" },
                redirectUrl: `${window.location.origin}/result`,
            });
            console.log("[PortOne] success", res);
            alert("결제 성공");
        } catch (e: any) {
            console.error("[PortOne] error", e);
            alert(e.message || "결제 오류");
        }
    };

    return (
        <button onClick={onClick} style={{ padding: "10px 16px", borderRadius: 8 }}>
            결제 테스트
        </button>
    );
}
