/**
 * Converts JS scenario modules (with config/next functions) to JSON
 * with configByChoice / nextByChoice for backend compatibility.
 */
import { writeFileSync, mkdirSync } from 'fs'
import { dirname, join } from 'path'
import { fileURLToPath } from 'url'

import { coffeeScenario } from '../src/data/coffeeScenarioData.js'
import { restaurantScenario } from '../src/data/restaurantScenario.js'
import { airportScenario } from '../src/data/airportScenario.js'
import { hotelScenario } from '../src/data/hotelScenario.js'

const __dirname = dirname(fileURLToPath(import.meta.url))
const outDir = join(__dirname, '../src/data/scenarios')

const scenarios = [
  { name: 'coffee', data: coffeeScenario },
  { name: 'restaurant', data: restaurantScenario },
  { name: 'airport', data: airportScenario },
  { name: 'hotel', data: hotelScenario }
]

function getChoiceIds(step) {
  return step.choices?.options?.map((o) => o.id) ?? []
}

function configsEqual(a, b) {
  return JSON.stringify(a) === JSON.stringify(b)
}

function convertTask(task, choiceIds) {
  const result = { type: task.type }

  if (typeof task.config !== 'function') {
    result.config = task.config
    return result
  }

  if (choiceIds.length === 0) {
    result.config = task.config(undefined)
    return result
  }

  const byChoice = {}
  for (const id of choiceIds) {
    byChoice[id] = task.config(id)
  }

  const values = Object.values(byChoice)
  const allSame = values.every((v) => configsEqual(v, values[0]))

  if (allSame) {
    result.config = values[0]
  } else {
    result.configByChoice = byChoice
  }

  return result
}

function convertStep(step) {
  const choiceIds = getChoiceIds(step)
  const converted = {}

  for (const [key, value] of Object.entries(step)) {
    if (key === 'tasks' || key === 'next' || key === 'config') continue
    converted[key] = value
  }

  if (step.tasks) {
    converted.tasks = step.tasks.map((t) => convertTask(t, choiceIds))
  }

  if (typeof step.next === 'function') {
    if (choiceIds.length > 0) {
      const nextByChoice = {}
      for (const id of choiceIds) {
        nextByChoice[id] = step.next(id, [])
      }
      const values = Object.values(nextByChoice)
      const allSame = values.every((v) => v === values[0])
      if (allSame) {
        converted.next = values[0]
      } else {
        converted.nextByChoice = nextByChoice
      }
    } else {
      converted.next = step.next(undefined, [])
    }
  } else if (step.next !== undefined) {
    converted.next = step.next
  }

  return converted
}

function convertScenario(scenario) {
  const { steps, ...meta } = scenario
  const convertedSteps = {}
  for (const [stepId, step] of Object.entries(steps)) {
    convertedSteps[stepId] = convertStep(step)
  }
  return { ...meta, steps: convertedSteps }
}

mkdirSync(outDir, { recursive: true })

for (const { name, data } of scenarios) {
  const json = convertScenario(data)
  const path = join(outDir, `${name}.json`)
  writeFileSync(path, JSON.stringify(json, null, 2), 'utf-8')
  console.log(`Written ${path}`)
}
