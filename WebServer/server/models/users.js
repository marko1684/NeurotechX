const mongoose = require('mongoose');

const schema = new mongoose.Schema({
_id : mongoose.Schema.Types.ObjectId,
id:{
    type: Number,
    required: true
},
name:{
    type: String,
    required: true
},
location:{
    type: Object,
    required: true
},
notify:{
    type: Number,
    required: true
},
sleepy:{
    type: Number,
    required: true
}
}, {collection: "Users"});

const modelUser = mongoose.model('Users', schema);

async function pullUser(id) {
    const user = await modelUser.findOne({id: id}).exec();
    return user;
}

async function changeData(id, x, y){
    
    const doc = await modelUser.findOne({id:id}).exec();
    doc.location = {x : Number(x), y : Number(y)};
    // doc.location.y = Number(y);
    doc.save();
    return doc;    
}

async function checkNotify(){

}

module.exports = {
    pullUser,
    changeData,
    checkNotify,
};
