import { SystemStatisticsDtoOut } from '@app/generated';
import { Statistics } from '@app/model/Statistics';

export const parseStatistics = (data: SystemStatisticsDtoOut): Statistics => {
  return {
    ...data
  };
};
