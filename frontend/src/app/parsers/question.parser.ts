import { QuestionDtoOut } from '@app/generated';
import { Question } from '@app/model/Question';

export const parseQuestion = (question: QuestionDtoOut): Question => {
  return {
    id: question.id,
    name: question.placeholder,
    label: question.cs
  };
};
