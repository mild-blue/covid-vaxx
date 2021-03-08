import { Question } from '../../model/Question';
import { AnswerDto } from '../../generated';

export const fromQuestionToAnswerGenerated = (question: Question): AnswerDto => {
  return {
    questionId: question.id,
    value: question.value === 'true' || question.value === true
  };
};
