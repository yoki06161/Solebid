import { OrderDetailList, OrderDetailPayment, OrderDetailShipping, OrderDetailSummary, OrderDetailTimeline } from "../components";
import { orderDetails } from "../components/mockData";

const OrderDetailPage = () => {
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
                        <OrderDetailList
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
