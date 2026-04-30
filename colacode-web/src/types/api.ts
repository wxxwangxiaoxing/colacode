export interface ApiResult<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  pageNo: number
  pageSize: number
  total: number
  records: T[]
}

export interface UserProfile {
  id: number
  userName: string
  nickName?: string
  email?: string
  phone?: string
  sex?: number
  avatar?: string
  status?: number
  introduce?: string
}

export interface LoginPayload {
  userName: string
  password: string
}

export interface RegisterPayload {
  userName: string
  nickName?: string
  email?: string
  phone?: string
  password: string
  sex?: number
  avatar?: string
  status?: number
  introduce?: string
}

export interface SubjectLabel {
  id: number
  labelName: string
  categoryId: number
  sortNum?: number
}

export interface SubjectCategory {
  id: number
  categoryName: string
  parentId?: number
  categoryType?: number
  children?: SubjectCategory[]
  labels?: SubjectLabel[]
}

export interface SubjectOption {
  optionType: string
  optionContent: string
  isCorrect?: number
}

export interface SubjectInfo {
  id: number
  subjectName: string
  subjectDiff?: number
  subjectType?: number
  subjectParse?: string
  subjectComment?: string
  browseCount?: number
  createdBy?: string
  contributeCount?: number
  categoryIds?: number[]
  labelIds?: number[]
  optionList?: SubjectOption[]
  correctAnswer?: string
  briefContent?: string
}

export interface PracticeSet {
  id: number
  setName: string
  description?: string
  status?: number
}

export interface PracticeDetail {
  id: number
  practiceId: number
  subjectId: number
  userAnswer?: string
  correctAnswer?: string
  isCorrect?: number
  timeUse?: number
}

export interface PracticeInfo {
  id: number
  setId: number
  userId: number
  totalScore?: number
  correctCount?: number
  wrongCount?: number
  submitTime?: string
  setName?: string
  timeUse?: number
  detail?: PracticeDetail
}

export interface PracticeAnswerItem {
  subjectId: number
  userAnswer: string
  timeUse?: number
}

export interface PracticeSubmitPayload {
  setId: number
  userId?: number
  answers: PracticeAnswerItem[]
}
