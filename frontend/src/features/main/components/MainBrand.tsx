import { Link } from "react-router-dom";
import type { MainBrandProps } from "../types/MainBrandProps";

const MainBrand = ({ brands }: MainBrandProps) => (
    <section className="max-w-[1440px] mx-auto px-6 pb-12">
        <h2 className="text-xl font-semibold text-gray-900 mb-6">
            인기 브랜드
        </h2>
        <div className="grid grid-cols-4 gap-6">
            {brands.map((brand) => (
                <Link
                    key={brand.id}
                    to=""
                    className="bg-white rounded-xl p-6 flex flex-col items-center cursor-pointer hover:shadow-lg transition-shadow"
                >
                    <img
                        src={brand.logo}
                        alt={brand.name}
                        className="w-24 h-24 object-contain mb-4"
                    />
                    <h3 className="text-lg font-medium text-gray-900">
                        {brand.name}
                    </h3>
                </Link>
            ))}
        </div>
    </section>
);

export default MainBrand;