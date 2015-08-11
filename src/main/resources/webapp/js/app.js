//model
var Page = {
    list: function () {
        return m.request({method: "GET", url: "api/deck/sample"});
    }
};

var vm = {
    cards: Page.list()
}

var Demo = {
    //controller
    controller: function () {
        var cards = Page.list()
        return {
            pages: cards,
            rotate: function () {
                vm.cards = m.request({method: "GET", url: "api/deck/russian"});
            }
        }
    },

    //view
    view: function (ctrl) {
        return m("div", [
            vm.cards().map(function (card) {
                var trustedDiv = function (title) {
                    return m("div", m("span", m.trust(title)))
                };

                return m("p", {class: "card"}, [
                    trustedDiv(card.question), trustedDiv(card.answer)
                ]);
            }),
            m("button", {onclick: ctrl.rotate}, "Rotate links"),

        ]);
    }
};

//initialize
m.mount(document.body, Demo);