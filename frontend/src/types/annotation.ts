export interface Annotation {
  id: string;
  pageId: string;
  editorEmail: string;
  spatialCoordinates: string | null;
  content: string;
  resolved: string;
  createdAt: string;
}

export interface AnnotationCreateRequest {
  editorEmail: string;
  spatialCoordinates: string | null;
  content: string;
}
