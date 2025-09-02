import type { MainHeroSectionProps } from "../types/MainHeroSectionProps";

const MainHeroSection = ({ image }: MainHeroSectionProps) => {
    return (
        <div className="relative bg-gradient-to-r from-gray-50 to-transparent overflow-hidden">
            <div className="max-w-[1440px] mx-auto px-6 py-16">
                <div className="grid grid-cols-2 gap-8 items-center">
                    <div className="space-y-6">
                        <h2 className="text-4xl font-bold text-gray-900">
                            신발의 가치를
                            <br />
                            새롭게 발견하세요
                        </h2>
                        <p className="text-lg text-gray-600">
                            한정판 스니커즈부터 클래식 모델까지,
                            <br />
                            투명한 경매를 통해 원하는 신발을 만나보세요.
                        </p>
                    </div>
                    <div className="relative">
                        <img
                            src={image}
                            alt="Hero"
                            className="w-full h-[400px] object-cover rounded-lg"
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MainHeroSection;