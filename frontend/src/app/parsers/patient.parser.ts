import { Patient } from '../model/Patient';
import { AnswerDtoOut, PatientDtoOut } from '@app/generated';
import { AnsweredQuestion } from '@app/model/AnsweredQuestion';
import { ZipCodePipe } from '@app/pipes/zip-code/zip-code.pipe';

export const parsePatient = (data: PatientDtoOut, questions: AnsweredQuestion[]): Patient => {
  const answeredQuestions = data.answers.map(a => parseAnsweredQuestion(a, questions));
  const zipCodePipe = new ZipCodePipe();

  return {
    ...data,
    zipCode: data.zipCode ? zipCodePipe.transform(`${data.zipCode}`) : '',
    questionnaire: answeredQuestions.filter(notEmpty),
    insuranceCompany: data.insuranceCompany,
    vaccinatedOn: data.vaccinated?.vaccinatedOn ? new Date(data.vaccinated.vaccinatedOn) : undefined,
    verified: !!data.dataCorrect?.dataAreCorrect,
    indication: data.indication ?? '',
    personalNumber: data.personalNumber ?? '',
    insuranceNumber: data.insuranceNumber ?? '',
    isForeigner: !data.personalNumber
  };
};

export const parseAnsweredQuestion = (data: AnswerDtoOut, questions: AnsweredQuestion[]): AnsweredQuestion | undefined => {
  const question = questions.find(q => q.id === data.questionId);
  if (!question) {
    return undefined;
  }

  return {
    id: question.id,
    label: question.label,
    name: question.name,
    answer: data.value
  };
};

// eslint-disable-next-line prefer-arrow/prefer-arrow-functions
function notEmpty<TValue>(value: TValue | undefined): value is TValue {
  return value !== undefined;
}
