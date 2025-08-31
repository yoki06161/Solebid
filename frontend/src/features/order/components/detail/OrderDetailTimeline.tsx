import type { OrderDetailTimelineProps } from "../../types/OrderTimeline";

const OrderDetailTimeline = ({ timeline }: OrderDetailTimelineProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                주문 상태 변경 이력
            </h3>
            <div className="space-y-4">
                {timeline.map((step, index) => (
                    <div
                        key={index}
                        className="flex items-start space-x-3">
                        <div
                            className={
                                `w-3 h-3 rounded-full mt-1 
                                ${step.completed
                                    ? "bg-green-500"
                                    : "bg-gray-300"
                                }`
                            }
                        ></div>
                        <div className="flex-1">
                            <div
                                className={
                                    `font-medium 
                                    ${step.completed
                                        ? "text-gray-900"
                                        : "text-gray-500"
                                    }`
                                }
                            >
                                {step.status}
                            </div>
                            <div className="text-sm text-gray-600">
                                {step.date}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default OrderDetailTimeline;