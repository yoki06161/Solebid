import type { BrandPopularProps } from "../../types/brand/BrandPopular";

const BrandPopular: React.FC<BrandPopularProps> = ({ brands }) => (
    <section className="mb-12">
        <h2 className="text-2xl font-bold mb-8">
            인기 브랜드
        </h2>
        <div className="grid grid-cols-4 gap-6">
            {brands.map((brand) => (
                <a
                    key={brand.id}
                    href="#"
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
                </a>
            ))}
        </div>
    </section>
);

export default BrandPopular;