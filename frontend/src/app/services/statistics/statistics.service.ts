import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SystemStatisticsDtoOut } from '@app/generated';
import { environment } from '@environments/environment';
import { map } from 'rxjs/operators';
import { Statistics } from '@app/model/Statistics';
import { parseStatistics } from '@app/parsers/statistics.parser';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private _statisticsnKey = 'questions';
  private _statisticsSubject: BehaviorSubject<Statistics | undefined> = new BehaviorSubject<Statistics | undefined>(undefined);
  public statistics: Observable<Statistics | undefined> = this._statisticsSubject.asObservable();

  constructor(private _http: HttpClient) {
    this._initStatistics();
  }

  public async loadStatistics(): Promise<Statistics> {
    return this._http.get<SystemStatisticsDtoOut>(
      `${environment.apiUrl}/statistics`
    ).pipe(
      map(response => {
        const statistics = parseStatistics(response);

        this._statisticsSubject.next(statistics);
        localStorage.setItem(this._statisticsnKey, JSON.stringify(statistics));

        return statistics;
      })
    ).toPromise();
  }

  private _initStatistics(): void {
    const value = localStorage.getItem(this._statisticsnKey);
    const savedStatistics = value ? JSON.parse(value) : undefined;
    this._statisticsSubject.next(savedStatistics);
  }
}
