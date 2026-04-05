import "./styles.css";
import { loadPackageFromJsonString } from "./lib/package/import/loadPackage";
import { downloadPackageJson } from "./lib/package/export/exportPackage";
import { PackageValidationIssue, PortableDrillPackage, StudioPackageListItem, StudioPackageViewModel } from "./lib/package/types";
import validSampleJson from "./lib/package/samples/valid-sample.json";
import invalidSampleJson from "./lib/package/samples/invalid-sample.json";

interface LoadedPackageRecord {
  listItem: StudioPackageListItem;
  raw: PortableDrillPackage;
  viewModel: StudioPackageViewModel;
  issues: PackageValidationIssue[];
}

const app = document.querySelector<HTMLDivElement>("#app");
if (!app) throw new Error("Missing #app");

const state: {
  packages: LoadedPackageRecord[];
  selectedPackageId?: string;
  selectedDrillId?: string;
  selectedPhaseId?: string;
  latestIssues: PackageValidationIssue[];
} = {
  packages: [],
  latestIssues: [],
};

function seedSamples(): void {
  const valid = loadPackageFromJsonString(JSON.stringify(validSampleJson), "sample", "valid-sample.json");
  if (valid.ok && valid.raw && valid.viewModel && valid.validation) {
    state.packages.push({
      listItem: { id: valid.packageId!, title: valid.viewModel.title, source: "sample", fileName: valid.fileName },
      raw: valid.raw,
      viewModel: valid.viewModel,
      issues: valid.validation.issues,
    });
    state.selectedPackageId = valid.packageId;
  }

  const invalid = loadPackageFromJsonString(JSON.stringify(invalidSampleJson), "sample", "invalid-sample.json");
  if (!invalid.ok && invalid.validation) {
    state.latestIssues = invalid.validation.issues;
  }
}

seedSamples();
render();

function render(): void {
  const current = state.packages.find((p) => p.listItem.id === state.selectedPackageId);
  const selectedDrill = current?.viewModel.drills.find((d) => d.drillId === (state.selectedDrillId ?? current?.viewModel.drills[0]?.drillId));
  const selectedPhase = selectedDrill?.phases.find((p) => p.phaseId === (state.selectedPhaseId ?? selectedDrill?.phases[0]?.phaseId));
  if (current && !state.selectedDrillId) state.selectedDrillId = selectedDrill?.drillId;
  if (selectedPhase && !state.selectedPhaseId) state.selectedPhaseId = selectedPhase.phaseId;

  app.innerHTML = `
  <div class="app">
    <div class="topbar">
      <button id="importBtn">Import Package</button>
      <button id="loadSampleBtn">Reload Sample</button>
      <button id="exportBtn" ${current ? "" : "disabled"}>Export Package</button>
      <span class="status">Save status: local-only placeholder</span>
      <input type="file" id="importInput" accept="application/json,.json" style="display:none" />
    </div>
    <div class="layout">
      <section class="panel" id="leftPanel">
        <h3>Package Library</h3>
        <div class="list">
          ${state.packages
            .map(
              (entry) => `
            <button class="card ${entry.listItem.id === state.selectedPackageId ? "selected" : ""}" data-package-id="${entry.listItem.id}">
              <strong>${entry.listItem.title}</strong><br/>
              <span class="muted">${entry.listItem.source}${entry.listItem.fileName ? ` • ${entry.listItem.fileName}` : ""}</span>
            </button>
          `,
            )
            .join("")}
        </div>
      </section>
      <section class="panel" id="centerPanel">
        <h3>Workspace</h3>
        ${selectedDrill ? `<div class="card"><strong>${selectedDrill.name}</strong><div>${selectedDrill.description ?? "No description"}</div><div class="muted">viewType: ${selectedDrill.viewType}</div></div>` : "<div class='muted'>Select a package</div>"}
        <h4>Phases</h4>
        <div class="list">
          ${selectedDrill?.phases
            .map(
              (phase) => `
            <button class="card ${phase.phaseId === state.selectedPhaseId ? "selected" : ""}" data-phase-id="${phase.phaseId}">
              <div class="phase-row"><span>${phase.order}. ${phase.name}</span><span>${phase.durationMs}ms</span></div>
            </button>`,
            )
            .join("") ?? ""}
        </div>
        <h4>Selected Phase</h4>
        ${selectedPhase ? `<div class="card"><div><strong>${selectedPhase.name}</strong></div><div class='muted'>${selectedPhase.phaseId}</div><div>Joint count: ${selectedPhase.jointCount}</div><div>Source image: ${selectedPhase.sourceImageUri ?? "none"}</div></div>` : "<div class='muted'>No phase selected</div>"}
        <h4>Package Validation Summary</h4>
        <div class="card"><div>Errors: ${(current?.issues ?? []).filter((x) => x.severity === "error").length}</div><div>Warnings: ${(current?.issues ?? []).filter((x) => x.severity === "warning").length}</div></div>
      </section>
      <section class="panel" id="rightPanel">
        <h3>Inspector</h3>
        ${current ? `<div class="card"><strong>Manifest</strong><div>ID: ${current.raw.manifest.packageId}</div><div>Title: ${current.raw.manifest.title}</div><div class="muted">${current.viewModel.manifestSummary || "No author/date metadata"}</div></div>` : ""}
        <div class="card"><strong>Asset Placeholder</strong><div>${selectedPhase?.sourceImageUri ?? "No source image ref"}</div></div>
        <div class="card"><strong>Pose Placeholder</strong><div>${selectedPhase ? `${selectedPhase.jointCount} canonical joints mapped` : "No selected phase"}</div></div>
        <div class="card"><strong>Validation Issues</strong>
          ${(current?.issues ?? state.latestIssues)
            .map((issue) => `<div class="issue-${issue.severity}">[${issue.severity}] ${issue.code}: ${issue.message}${issue.path ? ` (${issue.path})` : ""}</div>`)
            .join("") || "<div class='muted'>No issues</div>"}
        </div>
      </section>
    </div>
  </div>`;

  wireEvents(current?.raw);
}

function wireEvents(currentRaw?: PortableDrillPackage): void {
  document.querySelectorAll<HTMLButtonElement>("[data-package-id]").forEach((button) => {
    button.onclick = () => {
      state.selectedPackageId = button.dataset.packageId;
      state.selectedDrillId = undefined;
      state.selectedPhaseId = undefined;
      render();
    };
  });
  document.querySelectorAll<HTMLButtonElement>("[data-phase-id]").forEach((button) => {
    button.onclick = () => {
      state.selectedPhaseId = button.dataset.phaseId;
      render();
    };
  });

  const importInput = document.getElementById("importInput") as HTMLInputElement;
  const importBtn = document.getElementById("importBtn") as HTMLButtonElement;
  importBtn.onclick = () => importInput.click();
  importInput.onchange = async () => {
    const file = importInput.files?.[0];
    if (!file) return;
    const text = await file.text();
    const result = loadPackageFromJsonString(text, "import", file.name);
    if (!result.ok || !result.raw || !result.viewModel || !result.validation) {
      state.latestIssues = result.validation?.issues ?? result.errors ?? [];
      alert("Import failed. See Validation Issues panel.");
      render();
      return;
    }

    const entry: LoadedPackageRecord = {
      listItem: { id: result.packageId!, title: result.viewModel.title, source: "import", fileName: file.name },
      raw: result.raw,
      viewModel: result.viewModel,
      issues: result.validation.issues,
    };
    state.packages = [...state.packages.filter((x) => x.listItem.id !== entry.listItem.id), entry];
    state.selectedPackageId = entry.listItem.id;
    state.selectedDrillId = undefined;
    state.selectedPhaseId = undefined;
    state.latestIssues = result.validation.issues;
    render();
  };

  const exportBtn = document.getElementById("exportBtn") as HTMLButtonElement;
  exportBtn.onclick = () => {
    if (currentRaw) downloadPackageJson(currentRaw);
  };

  const sampleBtn = document.getElementById("loadSampleBtn") as HTMLButtonElement;
  sampleBtn.onclick = () => {
    state.packages = [];
    state.selectedDrillId = undefined;
    state.selectedPhaseId = undefined;
    seedSamples();
    render();
  };
}
