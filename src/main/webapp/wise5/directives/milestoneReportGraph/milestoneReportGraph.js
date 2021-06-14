'use strict';

class MilestoneReportGraphController {
  constructor($filter, UtilService) {
    this.$translate = $filter('translate');
    this.UtilService = UtilService;
  }

  $onInit() {
    this.graphData = [];
    this.categories = [];
    this.defaultColor = 'rgb(194, 24, 91)';
    if (this.name == null) {
      this.name = this.id;
    }
    this.calculateGraphDataAndCategories();
    this.setConfig();
  }

  setConfig() {
    const teamLabel = this.$translate('teams');
    this.config = {
      options: {
        chart: {
          type: 'column',
          height: 200,
          style: {
            fontFamily: 'Roboto,Helvetica Neue,sans-serif'
          }
        },
        title: {
          text: this.name,
          style: {
            fontSize: '14px',
            fontWeight: '500',
            color: this.titleColor ? this.titleColor : this.defaultColor
          }
        },
        plotOptions: {
          series: {
            dataLabels: {
              enabled: true,
              format: '{y}%'
            }
          },
          column: {
            dataLabels: {
              style: {
                fontSize: '10px'
              }
            }
          }
        },
        tooltip: {
          formatter: function() {
            return `<b>${this.series.name}<br/>${teamLabel}: ${this.point.count}</b>`;
          }
        }
      },
      xAxis: {
        categories: this.categories
      },
      yAxis: {
        title: {
          text: ''
        },
        labels: {
          enabled: false
        }
      },
      series: this.series,
      func: function(chart) {
        // temporary fix to ensure graphs are correctly resized to fit their container width
        setTimeout(function() {
          chart.reflow();
        }, 250);
      }
    };
  }

  calculateGraphDataAndCategories() {
    const series = [];
    const color = this.barColor ? this.barColor : this.defaultColor;
    const step = 100 / this.data.length / 100;
    let opacity = 0;
    for (const componentData of this.data) {
      opacity += step;
      const singleSeries = {
        name: this.UtilService.trimToLength(componentData.stepTitle, 26),
        color: this.UtilService.rgbToHex(color, opacity),
        data: this.getComponentSeriesData(componentData)
      };
      series.push(singleSeries);
    }
    this.series = series;
  }

  getComponentSeriesData(componentData) {
    const seriesData = [];
    const scoreKeys = Object.keys(componentData.counts);
    const scoreKeysSorted = scoreKeys.sort((a, b) => {
      return parseInt(a) - parseInt(b);
    });
    for (const scoreKey of scoreKeysSorted) {
      this.categories.push(scoreKey.toString());
      const scoreKeyCount = componentData.counts[scoreKey];
      const scoreKeyPercentage = Math.floor((100 * scoreKeyCount) / componentData.scoreCount);
      const scoreData = {
        y: scoreKeyPercentage,
        count: scoreKeyCount
      };
      seriesData.push(scoreData);
    }
    return seriesData;
  }
}

MilestoneReportGraphController.$inject = ['$filter', 'UtilService'];

const MilestoneReportGraph = {
  bindings: {
    id: '@',
    name: '@',
    titleColor: '@',
    barColor: '@',
    data: '<'
  },
  templateUrl: 'wise5/directives/milestoneReportGraph/milestoneReportGraph.html',
  controller: MilestoneReportGraphController,
  controllerAs: 'milestoneReportGraphCtrl'
};

export default MilestoneReportGraph;
