import { Answer } from '@app/model/Answer';
import { Question } from '@app/model/Question';

export const parseAnswerFromQuestion = (question: Question): Answer => {
  return {
    id: question.id,
    label: question.label,
    name: question.name,
    value: question.value === 'true' || question.value === true
  };
};
