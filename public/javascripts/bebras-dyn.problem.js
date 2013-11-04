var bebrasDynamicProblem = {};
var bebrasDynamicProblemImages = {};

var _bebras_dyn_problems_document_already_ready = false;

function bebras_dyn_problem_init(dyn_type, dynamicProblemClass, dynamicProblemImages) {
    var problem = new dynamicProblemClass('container-' + dyn_type, dynamicProblemImages);
}

function add_bebras_dyn_problem(key, images, problemInitializer) {
    bebrasDynamicProblem[key] = problemInitializer;
    bebrasDynamicProblemImages[key] = images;

    if (_bebras_dyn_problems_document_already_ready)
        bebras_dyn_problem_init(problemInitializer);
}

(function(){

    $(function() {
        //find all problems
        $('.problem').each(function() {
            var $problem = $(this);
            var dyn_type = $problem.find('dyn-type').text();

            var dynamicProblemClass = bebrasDynamicProblem[dyn_type];

            if (dynamicProblemClass)
                bebras_dyn_problem_init(dyn_type, dynamicProblemClass, bebrasDynamicProblemImages[dyn_type]);
        });

        _bebras_dyn_problems_document_already_ready = true;
    });

    function load_solution() {

    }

    register_solution_loader('bebras-dyn', load_solution);
})();