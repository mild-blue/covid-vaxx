import { AnsweredQuestion } from '@app/model/AnsweredQuestion';
import { AnswerDto } from '@app/generated';

export const fromQuestionToAnswerGenerated = (question: AnsweredQuestion): AnswerDto => {
  return {
    questionId: question.id,
    value: question.answer === 'true' || question.answer === true
  };
};
