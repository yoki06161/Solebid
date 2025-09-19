import type { TemperatureCardProps } from "../types/TemperatureCardProps";

const TemperatureCard = ({ temperature }: TemperatureCardProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6 text-center">
            <div className="flex items-center justify-center mb-2">
                <i 
                    className="fas fa-thermometer-half text-orange-500 text-2xl mr-2" 
                    aria-hidden="true"
                    role="img"
                    aria-label="온도계"
                ></i>
                <div 
                    className="text-2xl font-bold text-orange-600"
                    aria-label={`사용자 온도 ${temperature.toFixed(1)}도`}
                >
                    {temperature.toFixed(1)}°C
                </div>
            </div>
            <div className="text-gray-600 text-sm">사용자 온도</div>
        </div>
    );
};

export default TemperatureCard;