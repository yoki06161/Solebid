import { useState } from "react";
import { presign, uploadToS3 } from "../services/uploads";
import { makeSafeFileName } from "../utils/naming";

const ALLOWED = ["image/jpeg", "image/png", "image/jpg"];

export function useUpload(max = 5) {
    const [files, setFiles] = useState<File[]>([]);
    const [previews, setPreviews] = useState<string[]>([]);
    const [keys, setKeys] = useState<string[]>([]);
    const [uploading, setUploading] = useState(false);

    async function addFiles(selected: File[], opts?: { userId?: number }) {
        if (files.length + selected.length > max) throw new Error(`최대 ${max}장`);

        for (const f of selected) {
            const t = f.type || "";
            if (!ALLOWED.includes(t)) throw new Error("이미지는 JPG/PNG만 업로드할 수 있습니다.");
        }

        setUploading(true);
        try {
            const nextFiles = [...files];
            const nextPreviews = [...previews];
            const nextKeys = [...keys];

            for (const f of selected) {
                const ct = f.type === "image/png" ? "image/png" : "image/jpeg";
                const safeName = makeSafeFileName(f.name, ct);
                const { key, putUrl } = await presign(safeName, ct, { userId: opts?.userId });
                await uploadToS3(putUrl, f, ct);

                nextFiles.push(f);
                nextPreviews.push(URL.createObjectURL(f));
                nextKeys.push(key);
            }

            setFiles(nextFiles);
            setPreviews(nextPreviews);
            setKeys(nextKeys);
            return { files: nextFiles, previews: nextPreviews, keys: nextKeys };
        } finally {
            setUploading(false);
        }
    }

    function removeAt(i: number) {
        if (previews[i]) URL.revokeObjectURL(previews[i]);
        const nextFiles = files.filter((_, idx) => idx !== i);
        const nextPreviews = previews.filter((_, idx) => idx !== i);
        const nextKeys = keys.filter((_, idx) => idx !== i);
        setFiles(nextFiles);
        setPreviews(nextPreviews);
        setKeys(nextKeys);
        return { files: nextFiles, previews: nextPreviews, keys: nextKeys };
    }

    function clearAll() {
        previews.forEach((p) => URL.revokeObjectURL(p));
        setFiles([]);
        setPreviews([]);
        setKeys([]);
    }

    return { files, previews, keys, uploading, addFiles, removeAt, clearAll };
}
