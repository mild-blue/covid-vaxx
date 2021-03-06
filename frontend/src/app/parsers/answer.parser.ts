import { YesNoQuestion } from '../model/PatientInfo';
import { Answer } from '../model/Patient';

export const parseAnswerFromQuestion = (question: YesNoQuestion): Answer => {
  return {
    label: question.label,
    value: question.value ?? false
  };
};
