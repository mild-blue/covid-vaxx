import { Patient } from '../model/Patient';
import { AnswerDto, PatientDtoOut } from '@app/generated';
import { AnsweredQuestion } from '@app/model/AnsweredQuestion';
import { parseInsuranceCompany } from '@app/parsers/insurance.parser';
import { ZipCodePipe } from '@app/pipes/zip-code/zip-code.pipe';

export const parsePatient = (data: PatientDtoOut, questions: AnsweredQuestion[]): Patient => {
  const answeredQuestions = data.answers.map(a => parseAnsweredQuestion(a, questions));
  const zipCodePipe = new ZipCodePipe();

  return {
    ...data,
    zipCode: data.zipCode ? zipCodePipe.transform(`${data.zipCode}`) : '',
    questionnaire: answeredQuestions.filter(notEmpty),
    insuranceCompany: parseInsuranceCompany(data.insuranceCompany),
    created: new Date(data.created),
    updated: new Date(data.updated),
    vaccinatedOn: data.vaccinatedOn ? new Date(data.vaccinatedOn) : undefined
    // TODO: Add verifiedOn
  };
};

export const parseAnsweredQuestion = (data: AnswerDto, questions: AnsweredQuestion[]): AnsweredQuestion | undefined => {
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
