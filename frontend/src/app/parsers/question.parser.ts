import { QuestionDtoOut } from '@app/generated';
import { AnsweredQuestion } from '@app/model/AnsweredQuestion';

export const parseQuestion = (question: QuestionDtoOut): AnsweredQuestion => {
  return {
    id: question.id,
    name: question.placeholder,
    label: question.cs
  };
};
