
import QtQuick 2.7
import QtQuick.Controls 2.0

ApplicationWindow {
    visible: true

    /*
     * Multiline comment
     */
    Text {
        text: "string type 1"
    }

    // comment
    function testfunc() {
        console.log('string type 2');
    }
}
