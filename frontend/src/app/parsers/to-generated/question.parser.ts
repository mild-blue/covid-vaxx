import {AnsweredQuestion} from '@app/model/AnsweredQuestion';
import {AnswerDtoOut} from '@app/generated';

export const fromQuestionToAnswerGenerated = (question: AnsweredQuestion): AnswerDtoOut => {
  return {
    questionId: question.id,
    value: question.answer === 'true' || question.answer === true
  };
};
