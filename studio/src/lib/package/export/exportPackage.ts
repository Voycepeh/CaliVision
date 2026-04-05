import { PortableDrillPackage } from "../types";

export function createPackageJson(pkg: PortableDrillPackage): string {
  return JSON.stringify(pkg, null, 2);
}

export function downloadPackageJson(pkg: PortableDrillPackage, fallbackName = "drill-package"): void {
  const json = createPackageJson(pkg);
  const blob = new Blob([json], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  const safeName = pkg.manifest?.packageId || fallbackName;
  anchor.href = url;
  anchor.download = `${safeName}-export.json`;
  anchor.click();
  URL.revokeObjectURL(url);
}
