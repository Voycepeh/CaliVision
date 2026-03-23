# Class Diagram (Key Architecture Classes)

```mermaid
classDiagram
  class LiveCoachingViewModel {
    <<Coordinator>>
    +onRecordingFinalized(uri)
    +stopSession(callback)
  }

  class SessionRepository {
    <<PersistenceBoundary>>
    +saveSession(record)
    +updateAnnotatedExportStatus(sessionId, status)
  }

  class SessionBlobStorage {
    <<MediaStorage>>
    +saveRawVideoBlob()
    +saveAnnotatedVideoBlob()
  }

  class AnnotatedExportPipeline {
    <<Coordinator>>
    +export(...)
  }

  class AnnotatedExportNormalization {
    <<Processor>>
  }

  class MediaVerificationHelper {
    <<Validator>>
  }

  class MlKitVideoPoseFrameSource {
    <<FrameSource>>
  }

  class MotionAnalysisPipeline {
    <<Processor>>
  }

  class CalibrationEngine {
    <<Processor>>
  }

  LiveCoachingViewModel --> SessionRepository
  LiveCoachingViewModel --> AnnotatedExportPipeline
  LiveCoachingViewModel --> MotionAnalysisPipeline
  LiveCoachingViewModel --> MediaVerificationHelper
  SessionRepository --> SessionBlobStorage
  AnnotatedExportPipeline --> AnnotatedExportNormalization
  AnnotatedExportPipeline --> MediaVerificationHelper
  MlKitVideoPoseFrameSource --> MotionAnalysisPipeline
  CalibrationEngine --> MotionAnalysisPipeline
```
