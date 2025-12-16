export enum Lifecycle {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  DEPRECATED = 'DEPRECATED'
}

export enum HttpMethod {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  PATCH = 'PATCH',
  DELETE = 'DELETE'
}

export enum ChangelogType {
  ADDED = 'ADDED',
  CHANGED = 'CHANGED',
  DEPRECATED = 'DEPRECATED',
  REMOVED = 'REMOVED',
  FIXED = 'FIXED'
}

export interface ApiSummary {
  id: string;
  name: string;
  baseUrl: string;
  version: string;
  ownerTeam: string;
  lifecycle: Lifecycle;
  updatedAt: string;
}

export interface ApiDetail extends ApiSummary {
  openApiUrl?: string;
  description?: string;
  createdAt: string;
  endpoints: Endpoint[];
  changelog: ChangelogEntry[];
}

export interface Endpoint {
  id: string;
  method: HttpMethod;
  path: string;
  description?: string;
  deprecated: boolean;
  createdAt: string;
}

export interface ChangelogEntry {
  id: string;
  type: ChangelogType;
  breaking: boolean;
  summary: string;
  details?: string;
  releasedAt: string;
}

export interface ApiCreateDto {
  name: string;
  baseUrl: string;
  version: string;
  ownerTeam: string;
  lifecycle: Lifecycle;
  openApiUrl?: string;
  description?: string;
}

export interface ApiUpdateDto extends ApiCreateDto {}

export interface EndpointCreateDto {
  method: HttpMethod;
  path: string;
  description?: string;
  deprecated?: boolean;
}

export interface ChangelogCreateDto {
  type: ChangelogType;
  breaking?: boolean;
  summary: string;
  details?: string;
  releasedAt: string;
}

export interface ApiListParams {
  q?: string;
  lifecycle?: Lifecycle;
  ownerTeam?: string;
  sort?: string;
  dir?: string;
}

export enum SyncMode {
  MERGE = 'MERGE',
  REPLACE = 'REPLACE'
}

export enum SyncStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED'
}

export enum BreakingChangeType {
  REMOVED_ENDPOINT = 'REMOVED_ENDPOINT',
  PATH_CHANGED = 'PATH_CHANGED',
  METHOD_CHANGED = 'METHOD_CHANGED'
}

export interface BreakingChange {
  type: BreakingChangeType;
  method: string;
  path: string;
  details?: string;
}

export interface ChangedEndpoint {
  current: Endpoint;
  proposed: Endpoint;
  changeDescription: string;
}

export interface Diff {
  addedEndpoints: Endpoint[];
  removedEndpoints: Endpoint[];
  changedEndpoints: ChangedEndpoint[];
}

export interface ImportResult {
  syncRunId: string;
  addedCount: number;
  updatedCount: number;
  deletedCount: number;
  breaksDetected: boolean;
  breakingChanges: BreakingChange[];
}

export interface ApiSyncRun {
  id: string;
  apiId: string;
  runAt: string;
  status: SyncStatus;
  mode: SyncMode;
  addedCount: number;
  updatedCount: number;
  deletedCount: number;
  breaksDetected: boolean;
  errorMessage?: string;
  breakingChanges: BreakingChange[];
}

