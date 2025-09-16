export function makeSafeFileName(originalName: string, contentType?: string): string {
    const isPng = (contentType || originalName).toLowerCase().includes("png");
    const ext = isPng ? "png" : "jpg";
    const suffix = Math.random().toString(36).slice(2, 8);
    return `img_${Date.now()}_${suffix}.${ext}`;
}

