import type { BrandListProps } from "../../types/brand/BrandListProps";
import BrandItem from "./BrandItem";

const BrandList: React.FC<BrandListProps> = ({ brandData, onBidClick }) => (
    <div className="bg-white rounded-xl p-8 mb-8">
        <div className="flex items-center mb-6">
            <img
                src={brandData.logo}
                alt={brandData.brand}
                className="w-16 h-16 object-contain mr-4"
            />
            <div>
                <h3 className="text-xl font-bold text-gray-900">
                    {brandData.brand}
                </h3>
                <p className="text-gray-600">
                    {brandData.description}
                </p>
            </div>
        </div>
        <div className="grid grid-cols-3 gap-6">
            {brandData.products.map((product) => (
                <BrandItem
                    key={product.id}
                    product={product}
                    onBidClick={onBidClick}
                />
            ))}
        </div>
    </div>
);

export default BrandList;