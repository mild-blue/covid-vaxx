import { Answer } from '@app/model/Answer';
import { Question } from '@app/model/Question';

export const parseAnswerFromQuestion = (question: Question): Answer => {
  return {
    label: question.label,
    value: question.value === 'true' || question.value === true
  };
};
