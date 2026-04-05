import { mapPortablePackageToViewModel } from "../mapping/mapPortablePackageToViewModel";
import { LocalPackageLoadResult, PortableDrillPackage } from "../types";
import { validatePortablePackage } from "../validation/validatePortablePackage";

export function loadPackageFromJsonString(
  content: string,
  source: "sample" | "import",
  fileName?: string,
): LocalPackageLoadResult {
  try {
    const parsed = JSON.parse(content) as PortableDrillPackage;
    const validation = validatePortablePackage(parsed);
    if (!validation.isValid) {
      return {
        ok: false,
        source,
        fileName,
        validation,
        errors: validation.issues.filter((x) => x.severity === "error"),
      };
    }

    const viewModel = mapPortablePackageToViewModel(parsed, source, fileName);
    return {
      ok: true,
      source,
      fileName,
      packageId: parsed.manifest.packageId,
      raw: parsed,
      viewModel,
      validation,
    };
  } catch {
    return {
      ok: false,
      source,
      fileName,
      errors: [{ severity: "error", code: "JSON_PARSE_ERROR", message: "Could not parse JSON file." }],
    };
  }
}
