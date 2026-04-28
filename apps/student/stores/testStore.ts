import { create } from 'zustand'
import type { Question, RiasecScores } from '@tentuin/supabase'

type TestStore = {
  // Persisted across navigation
  questions:    Question[]
  answers:      Record<string, number>   // questionId → 1-5
  currentIndex: number
  isActive:     boolean                  // test sedang berjalan

  // Actions
  setQuestions:    (q: Question[]) => void
  setAnswer:       (questionId: string, value: number) => void
  setCurrentIndex: (index: number) => void
  startTest:       () => void
  resetTest:       () => void
}

export const useTestStore = create<TestStore>((set) => ({
  questions:    [],
  answers:      {},
  currentIndex: 0,
  isActive:     false,

  setQuestions: (questions) => set({ questions }),
  setAnswer:    (questionId, value) =>
    set((s) => ({ answers: { ...s.answers, [questionId]: value } })),
  setCurrentIndex: (currentIndex) => set({ currentIndex }),
  startTest:    () => set({ isActive: true }),
  resetTest:    () => set({ questions: [], answers: {}, currentIndex: 0, isActive: false }),
}))
