import { useParams } from "react-router-dom";
import { OrderDetailItem, OrderDetailPayment, OrderDetailShipping, OrderDetailSummary, OrderDetailTimeline } from "../components";
import { orders } from "../components/mockData";

const OrderDetailPage = () => {
    const { orderId } = useParams<{ orderId: string }>();
    const orderDetails = orders.find(o => o.id === orderId);

    if (!orderDetails) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <h2 className="text-2xl font-bold text-gray-800">주문 정보를 찾을 수 없습니다.</h2>
                    <p className="text-gray-600 mt-2">요청하신 주문에 해당하는 정보가 없습니다.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <OrderDetailSummary
                    id={orderDetails.id}
                    date={orderDetails.date}
                    status={orderDetails.status}
                    statusColor={orderDetails.statusColor}
                    totalAmount={orderDetails.totalAmount}
                />
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    <div className="lg:col-span-2 space-y-6">
                        <OrderDetailItem
                            items={orderDetails.items}
                        />
                        <OrderDetailPayment
                            payment={orderDetails.payment}
                        />
                        <OrderDetailShipping
                            shipping={orderDetails.shipping}
                        />
                    </div>
                    <div className="space-y-6">
                        <OrderDetailTimeline
                            timeline={orderDetails.timeline}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OrderDetailPage;
