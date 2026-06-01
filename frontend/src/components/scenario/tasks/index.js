import PhraseBuilderTask from './PhraseBuilderTask.vue'
import FillBlankTask from './FillBlankTask.vue'
import WordOrderTask from './WordOrderTask.vue'
import TranslationTask from './TranslationTask.vue'
import MultipleChoiceTask from './MultipleChoiceTask.vue'
import MatchingPairsTask from './MatchingPairsTask.vue'


// Каждое задание принимает props: { config }
//событие: complete({ correct: boolean, userAnswer: any, score: number })

export const TaskRegistry = {
  'phrase-builder': PhraseBuilderTask,
  'fill-blank': FillBlankTask,
  'word-order': WordOrderTask,
  'translation': TranslationTask,
  'multiple-choice': MultipleChoiceTask,
  'matching': MatchingPairsTask
}

export function getTaskComponent(type) {
  return TaskRegistry[type] || null
}