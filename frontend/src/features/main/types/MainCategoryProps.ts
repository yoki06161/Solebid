export interface MainCategory {
    name: string;
    icon: string;
    count: string;
}

export interface MainCategoryProps {
    category: MainCategory;
    onClick: () => void;
}