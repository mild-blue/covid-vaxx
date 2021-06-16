import { Component } from '@angular/core';
import { StatisticsService } from '@app/services/statistics/statistics.service';
import { Statistics } from '@app/model/Statistics';

@Component({
  selector: 'app-info',
  templateUrl: './info.component.html',
  styleUrls: ['./info.component.scss']
})
export class InfoComponent {
  public statistics?: Statistics;

  constructor(private _statisticsService: StatisticsService) {
    this._statisticsService.statistics.subscribe(statistics => {
      this.statistics = statistics;
    });
  }

}
