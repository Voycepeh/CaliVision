import {
  PackageValidationIssue,
  PackageValidationResult,
  PortableDrillPackage,
} from "../types";

export const CANONICAL_JOINTS = [
  "nose",
  "left_eye",
  "right_eye",
  "left_ear",
  "right_ear",
  "left_shoulder",
  "right_shoulder",
  "left_elbow",
  "right_elbow",
  "left_wrist",
  "right_wrist",
  "left_hip",
  "right_hip",
  "left_knee",
  "right_knee",
  "left_ankle",
  "right_ankle",
] as const;

const canonicalSet = new Set<string>(CANONICAL_JOINTS);

export function validatePortablePackage(candidate: unknown): PackageValidationResult {
  const issues: PackageValidationIssue[] = [];
  if (!candidate || typeof candidate !== "object") {
    return {
      isValid: false,
      issues: [{ severity: "error", code: "PACKAGE_NOT_OBJECT", message: "Package must be a JSON object." }],
    };
  }

  const pkg = candidate as Partial<PortableDrillPackage>;

  if (!pkg.schemaVersion || typeof pkg.schemaVersion !== "string") {
    issues.push(issue("error", "SCHEMA_VERSION_REQUIRED", "schemaVersion is required.", "schemaVersion"));
  }

  if (!pkg.manifest || typeof pkg.manifest !== "object") {
    issues.push(issue("error", "MANIFEST_REQUIRED", "manifest is required.", "manifest"));
  } else {
    if (!pkg.manifest.packageId) issues.push(issue("error", "MANIFEST_PACKAGE_ID_REQUIRED", "manifest.packageId is required.", "manifest.packageId"));
    if (!pkg.manifest.title) issues.push(issue("error", "MANIFEST_TITLE_REQUIRED", "manifest.title is required.", "manifest.title"));
  }

  if (!Array.isArray(pkg.drills) || pkg.drills.length === 0) {
    issues.push(issue("error", "DRILLS_REQUIRED", "At least one drill is required.", "drills"));
    return withValidity(issues);
  }

  pkg.drills.forEach((drill, drillIndex) => {
    const drillPath = `drills[${drillIndex}]`;
    if (!drill.drillId) issues.push(issue("error", "DRILL_ID_REQUIRED", "drillId is required.", `${drillPath}.drillId`));
    if (!drill.name) issues.push(issue("error", "DRILL_NAME_REQUIRED", "drill name is required.", `${drillPath}.name`));

    if (!["FRONT", "SIDE", "BACK"].includes(drill.viewType)) {
      issues.push(issue("error", "DRILL_VIEW_TYPE_INVALID", "viewType must be FRONT, SIDE, or BACK.", `${drillPath}.viewType`));
    }

    if (!Array.isArray(drill.phases) || drill.phases.length === 0) {
      issues.push(issue("error", "PHASES_REQUIRED", "drill phases are required.", `${drillPath}.phases`));
      return;
    }

    const orderSet = new Set<number>();
    drill.phases.forEach((phase, phaseIndex) => {
      const phasePath = `${drillPath}.phases[${phaseIndex}]`;
      if (!phase.phaseId) issues.push(issue("error", "PHASE_ID_REQUIRED", "phaseId is required.", `${phasePath}.phaseId`));
      if (!phase.name) issues.push(issue("error", "PHASE_NAME_REQUIRED", "phase name is required.", `${phasePath}.name`));
      if (typeof phase.order !== "number") issues.push(issue("error", "PHASE_ORDER_REQUIRED", "phase order is required.", `${phasePath}.order`));
      if (orderSet.has(phase.order)) issues.push(issue("error", "PHASE_ORDER_DUPLICATE", "phase order must be unique.", `${phasePath}.order`));
      orderSet.add(phase.order);

      if (typeof phase.durationMs !== "number" || phase.durationMs <= 0) {
        issues.push(issue("error", "PHASE_DURATION_INVALID", "durationMs must be > 0.", `${phasePath}.durationMs`));
      } else if (phase.durationMs < 200) {
        issues.push(issue("warning", "PHASE_DURATION_SHORT", "Very short phase duration may be hard to coach.", `${phasePath}.durationMs`));
      }

      const joints = phase.pose?.joints;
      if (!joints || typeof joints !== "object") {
        issues.push(issue("error", "POSE_JOINTS_REQUIRED", "pose.joints is required.", `${phasePath}.pose.joints`));
      } else {
        Object.entries(joints).forEach(([jointName, coord]) => {
          const jointPath = `${phasePath}.pose.joints.${jointName}`;
          if (!canonicalSet.has(jointName)) {
            issues.push(issue("error", "JOINT_NAME_INVALID", `Joint '${jointName}' is not canonical.`, jointPath));
          }
          if (typeof coord.x !== "number" || coord.x < 0 || coord.x > 1) {
            issues.push(issue("error", "JOINT_X_INVALID", "Joint x must be normalized 0..1.", `${jointPath}.x`));
          }
          if (typeof coord.y !== "number" || coord.y < 0 || coord.y > 1) {
            issues.push(issue("error", "JOINT_Y_INVALID", "Joint y must be normalized 0..1.", `${jointPath}.y`));
          }
        });
      }

      const sourceImage = phase.assetRefs?.sourceImage;
      if (phase.assetRefs && typeof phase.assetRefs !== "object") {
        issues.push(issue("error", "ASSET_REFS_INVALID", "assetRefs must be an object.", `${phasePath}.assetRefs`));
      }
      if (sourceImage) {
        if (!sourceImage.uri) {
          issues.push(issue("error", "ASSET_URI_REQUIRED", "sourceImage.uri is required when source image is present.", `${phasePath}.assetRefs.sourceImage.uri`));
        }
        if (sourceImage.width !== undefined && sourceImage.width <= 0) {
          issues.push(issue("error", "ASSET_WIDTH_INVALID", "sourceImage.width must be > 0.", `${phasePath}.assetRefs.sourceImage.width`));
        }
      }
    });
  });

  return withValidity(issues);
}

function issue(
  severity: "error" | "warning",
  code: string,
  message: string,
  path?: string,
): PackageValidationIssue {
  return { severity, code, message, path };
}

function withValidity(issues: PackageValidationIssue[]): PackageValidationResult {
  return { isValid: !issues.some((x) => x.severity === "error"), issues };
}
