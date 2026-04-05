export type ValidationSeverity = "error" | "warning";

export interface PackageValidationIssue {
  severity: ValidationSeverity;
  code: string;
  message: string;
  path?: string;
}

export interface PackageValidationResult {
  isValid: boolean;
  issues: PackageValidationIssue[];
}

export interface LocalPackageLoadResult {
  ok: boolean;
  packageId?: string;
  fileName?: string;
  source: "sample" | "import";
  raw?: PortableDrillPackage;
  viewModel?: StudioPackageViewModel;
  validation?: PackageValidationResult;
  errors?: PackageValidationIssue[];
}

export interface PortableDrillPackage {
  schemaVersion: string;
  manifest: {
    packageId: string;
    title: string;
    description?: string;
    author?: string;
    createdAt?: string;
    updatedAt?: string;
  };
  drills: PortableDrill[];
}

export interface PortableDrill {
  drillId: string;
  name: string;
  description?: string;
  viewType: "FRONT" | "SIDE" | "BACK";
  phases: PortableDrillPhase[];
}

export interface PortableDrillPhase {
  phaseId: string;
  name: string;
  order: number;
  durationMs: number;
  pose: {
    joints: Record<string, PortableJointCoordinate>;
  };
  assetRefs?: {
    sourceImage?: {
      uri: string;
      width?: number;
      height?: number;
      mimeType?: string;
    };
  };
}

export interface PortableJointCoordinate {
  x: number;
  y: number;
  confidence?: number;
}

export interface StudioPackageListItem {
  id: string;
  title: string;
  source: "sample" | "import";
  fileName?: string;
}

export interface StudioPackageViewModel {
  id: string;
  title: string;
  schemaVersion: string;
  source: "sample" | "import";
  fileName?: string;
  manifestSummary: string;
  drills: StudioDrillViewModel[];
}

export interface StudioDrillViewModel {
  drillId: string;
  name: string;
  description?: string;
  viewType: string;
  phases: StudioPhaseViewModel[];
}

export interface StudioPhaseViewModel {
  phaseId: string;
  name: string;
  order: number;
  durationMs: number;
  jointCount: number;
  sourceImageUri?: string;
}
