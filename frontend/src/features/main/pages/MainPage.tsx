import { MainBrand, MainCategoryList, MainHeroSection, MainProductList } from '../components';
import { categories, heroImage, popularBrands, trendingProducts } from '../components/mockData';

const MainPage = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <MainHeroSection
        image={heroImage}
      />
      <MainCategoryList
        categories={categories}
      />
      <MainBrand
        brands={popularBrands}
      />
      <MainProductList
        products={trendingProducts}
      />
    </div>
  );
};

export default MainPage;