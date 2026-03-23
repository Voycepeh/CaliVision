# UI Flow

```mermaid
flowchart TD
  H[Home]
  D[Drill Select]
  L[Live Coaching]
  C[Countdown]
  R[Recording]
  P[Processing]
  RS[Replay / Results]
  HI[History]
  U[Imported Analysis]
  CAL[Calibration]

  H --> D
  H --> L
  H --> U
  H --> HI
  H --> CAL

  D --> C
  L --> C
  C --> R
  R --> P
  P --> RS

  HI --> RS
  U --> P
  CAL --> H
  RS --> H
```
