import { Question } from '../model/Question';
import { YesNoQuestion } from '../model/PatientInfo';

export const parseQuestion = (question: Question): YesNoQuestion => {
  return {
    id: question.id,
    name: question.placeholder,
    label: question.cs // todo: add english label (will depend on i18n strategy)
  };
};
