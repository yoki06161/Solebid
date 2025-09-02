import { Link } from "react-router-dom";
import type { MainProductProps } from "../types/MainProductProps";

const MainProduct = ({ product }: MainProductProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer overflow-hidden">
            <Link to={``}>
                <img
                    src={product.image}
                    alt={product.name}
                    className="w-full h-64 object-cover"
                />
            </Link>
            <div className="p-4">
                <h4 className="text-lg font-medium text-gray-900 truncate">
                    {product.name}
                </h4>
                <div className="mt-2 flex items-center justify-between">
                    <div>
                        <p className="text-sm text-gray-500">
                            현재가
                        </p>
                        <p className="text-lg font-semibold text-blue-600">
                            ₩{product.price.toLocaleString()}
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-sm text-gray-500">
                            {product.bidCount}명 참여
                        </p>
                        <p className="text-sm font-medium text-red-500">
                            {product.timeLeft} 남음
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MainProduct;