/*
 * Statistical summary charts (sentiment, language, visibility and engagement).
 * @author Owen Van Esbroeck
 */

function opts(title) {
    return {
      responsive: true,
      plugins: {
        legend: {
          position: 'top',
        },
        title: {
          display: true,
          text: title
        }
      },
      maintainAspectRatio: false 
    }
}

function data() {
    
    var sentiment_data = JSON.parse(document.getElementById('sentiment-div').dataset.sentiments);
    var sentiment_canvas = document.getElementById('sentiment-chart').getContext('2d');
    var sentiment_entries = Object.entries(sentiment_data)
    var sentiment_chart = new Chart(sentiment_canvas, {    
    type: 'pie',
    data: {
        labels: sentiment_entries.map(x => x[0]),
        datasets: [{
            //label: 'Sentiments',
            data: sentiment_entries.map(x => x[1]),
        }]
    },
    options: opts("Sentiments")
    });
    //console.log(sentiment_chart)
    var lang_data = JSON.parse(document.getElementById('language-div').dataset.languages);
    var lang_canvas = document.getElementById('language-chart').getContext('2d');
    var lang_entries = Object.entries(lang_data)
    var lang_chart = new Chart(lang_canvas, {    
    type: 'pie',
    data: {
        labels: lang_entries.map(x => x[0]),
        datasets: [{
            //label: 'languages',
            data: lang_entries.map(x => x[1]),
        }]
    },
    options: opts("Languages")
    });
    //
    var vis_data = JSON.parse(document.getElementById('visibility-div').dataset.visibility);
    var vis_canvas = document.getElementById('visibility-chart').getContext('2d');
    var vis_entries = Object.entries(vis_data)
    var vis_chart = new Chart(vis_canvas, {    
    type: 'pie',
    data: {
        labels: vis_entries.map(x => x[0]),
        datasets: [{
            //label: 'visibility',
            data: vis_entries.map(x => x[1]),
        }]
    },
    options: opts("Visibility")
    });

    var eng_data = JSON.parse(document.getElementById('engagement-div').dataset.engagement);
    var eng_canvas = document.getElementById('engagement-chart').getContext('2d');
    var eng_keys = Object.keys(eng_data)
    var eng_chart = new Chart(eng_canvas, {    
    type: 'bar',
    data: {
        labels: eng_keys,
        datasets: [
            {
            label: "Minimum",
            data: eng_keys.map(x => 
            { return eng_data[x]["min"];}
            ),},
            {
            label: "Average",
            data: eng_keys.map(x => 
            { return eng_data[x]["avg"];}
            ),},
            {
            label: "Maximum",
            data: eng_keys.map(x => 
            { return eng_data[x]["max"];}
            ),},
        ]
    },
    options: opts("Engagment")
    });
}


document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM fully loaded and parsed!");
    data();
});

