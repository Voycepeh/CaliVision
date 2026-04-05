import { PortableDrillPackage, StudioPackageViewModel } from "../types";

export function mapPortablePackageToViewModel(
  pkg: PortableDrillPackage,
  source: "sample" | "import",
  fileName?: string,
): StudioPackageViewModel {
  return {
    id: pkg.manifest.packageId,
    title: pkg.manifest.title,
    schemaVersion: pkg.schemaVersion,
    source,
    fileName,
    manifestSummary: [pkg.manifest.author, pkg.manifest.createdAt].filter(Boolean).join(" • "),
    drills: pkg.drills.map((drill) => ({
      drillId: drill.drillId,
      name: drill.name,
      description: drill.description,
      viewType: drill.viewType,
      phases: [...drill.phases]
        .sort((a, b) => a.order - b.order)
        .map((phase) => ({
          phaseId: phase.phaseId,
          name: phase.name,
          order: phase.order,
          durationMs: phase.durationMs,
          jointCount: Object.keys(phase.pose.joints).length,
          sourceImageUri: phase.assetRefs?.sourceImage?.uri,
        })),
    })),
  };
}
