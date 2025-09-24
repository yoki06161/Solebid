import { useState, type ReactNode } from "react";
import Modal from "../../components/Modal.tsx";
import { ModalContext } from "./modal.ts";

export const ModalProvider = ({ children }: { children: ReactNode }) => {
    const [modalContent, setModalContent] = useState<ReactNode | null>(null);

    const openModal = (content: ReactNode) => {
        setModalContent(content);
    };

    const closeModal = () => {
        setModalContent(null);
    };

    return (
        <ModalContext.Provider
            value={{ openModal, closeModal }}>
            {children}
            {modalContent && (
                <Modal isOpen={!!modalContent} onClose={closeModal}>
                    {modalContent}
                </Modal>
            )}
        </ModalContext.Provider>
    );
};
